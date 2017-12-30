import java.awt.Color;
import java.util.Arrays;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

    private int width, height;
    private int[][] rgb;
    private double[][] energy;
    private boolean isTransposed;

    public SeamCarver(Picture picture) {
        if (picture != null) {
            width = picture.width();
            height = picture.height();
            rgb = new int[height][width];
            energy = new double[height][width];
            isTransposed = false;

            for (int row = 0; row < height; ++row)
                for (int col = 0; col < width; ++col)
                    rgb[row][col] = picture.get(col, row).getRGB();

            Arrays.fill(energy[0], 1000);
            for (int row = 1; row + 1 < height; ++row) {
                energy[row][0] = 1000;
                for (int col = 1; col + 1 < width; ++col)
                    energy[row][col] = dualGradient(row, col);
                energy[row][width - 1] = 1000;
            }
            Arrays.fill(energy[height - 1], 1000);
        } else throw new IllegalArgumentException();
    }

    public Picture picture() {
        if (isTransposed)
            transpose();
        Picture picture = new Picture(width, height);
        for (int row = 0; row < height; ++row)
            for (int col = 0; col < width; ++col)
                picture.set(col, row, new Color(rgb[row][col]));
        return picture;
    }

    public int width() { return isTransposed ? height : width; }

    public int height() { return isTransposed ? width : height; }

    public double energy(int x, int y) {
        if (isValid(x, y))
            return isTransposed ? energy[x][y] : energy[y][x];
        else throw new IllegalArgumentException();
    }

    public int[] findHorizontalSeam() {
        if (!isTransposed)
            transpose();
        return findSeam();
    }

    public int[] findVerticalSeam() {
        if (isTransposed)
            transpose();
        return findSeam();
    }

    public void removeHorizontalSeam(int[] seam) {
        if (!isTransposed)
            transpose();
        removeSeam(seam);
    }

    public void removeVerticalSeam(int[] seam) {
        if (isTransposed)
            transpose();
        removeSeam(seam);
    }

    private int[] findSeam() {
        double[][] energyTo = new double[height][width];
        int[][] colTo = new int[height][width];
        Arrays.fill(energyTo[0], 1000);
        for (int row = 1; row < height; ++row)
            Arrays.fill(energyTo[row], Double.POSITIVE_INFINITY);

        for (int vrow = 0; vrow + 1 < height; ++vrow) {
            for (int vcol = 1; vcol + 1 < width; ++vcol) {
                for (int i = 0; i < 3; ++i) {
                    int wrow = vrow + 1;
                    int wcol = vcol + i - 1;
                    double e = energyTo[vrow][vcol] + energy[wrow][wcol];
                    if (energyTo[wrow][wcol] > e) {
                        energyTo[wrow][wcol] = e;
                        colTo[wrow][wcol] = vcol;
                    }
                }
            }
        }

        double minEnergy = Double.POSITIVE_INFINITY;
        int minCol = 0;
        double[] base = energyTo[height - 1];
        for (int col = 1; col + 1 < width; ++col) {
            double e = base[col];
            if (e < minEnergy) {
                minEnergy = e;
                minCol = col;
            }
        }

        int[] seam = new int[height];
        int col = minCol;
        for (int row = height - 1; row >= 0; --row) {
            seam[row] = col;
            col = colTo[row][col];
        }
        return seam;
    }

    private void removeSeam(int[] seam) {
        if (width > 1 && isValid(seam)) {
            --width;
            for (int row = 0; row < height; ++row) {
                int col = seam[row];
                System.arraycopy(rgb[row], col + 1,
                                 rgb[row], col, width - col);
                System.arraycopy(energy[row], col + 1,
                                 energy[row], col, width - col);
            }
            for (int row = 1; row + 1 < height; ++row) {
                for (int i = 0; i < 2; ++i) {
                    int col = seam[row] + i - 1;
                    if (col > 0 && col + 1 < width)
                        energy[row][col] = dualGradient(row, col);
                }
            }
        } else throw new IllegalArgumentException();
    }

    private double dualGradient(int row, int col) {
        int dx = gradient(rgb[row][col - 1], rgb[row][col + 1]);
        int dy = gradient(rgb[row - 1][col], rgb[row + 1][col]);
        return Math.sqrt(dx + dy);
    }

    private int gradient(int a, int b) {
        int e = 0;
        for (int i = 0; i < 3; ++i, a >>= 8, b >>= 8) {
            int d = (a & 0xff) - (b & 0xff);
            e += d * d;
        }
        return e;
    }

    private void transpose() {
        int[][] rgbCopy = new int[width][height];
        double[][] energyCopy = new double[width][height];

        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
                rgbCopy[col][row] = rgb[row][col];
                energyCopy[col][row] = energy[row][col];
            }
        }

        rgb = rgbCopy;
        energy = energyCopy;

        int temp = width;
        width = height;
        height = temp;

        isTransposed = !isTransposed;
    }

    private boolean isValid(int x, int y) {
        if (x >= 0 && y >= 0) {
            return isTransposed ? x < height && y < width
                                : x < width && y < height;
        } else return false;
    }

    private boolean isValid(int[] seam) {
        if (seam != null && seam.length == height) {
            for (int col : seam)
                if (col < 0 || col >= width)
                    return false;
            for (int i = 0; i + 1 < seam.length; ++i) {
                int diff = seam[i] - seam[i + 1];
                if (diff * diff > 1)
                    return false;
            }
            return true;
        } else return false;
    }

}
