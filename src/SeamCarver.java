import java.awt.Color;
import java.util.Arrays;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

    private int arrWidth, arrHeight;
    private int width, height;
    private long[] rgbe, rgbeT;
    private final int[] colTo;
    private final long[] energyTo;
    private boolean isTransposed;

    public SeamCarver(Picture picture) {
        if (picture != null) {
            arrWidth = picture.width();
            arrHeight = picture.height();
            width = arrWidth;
            height = arrHeight;
            rgbe = new long[width * height];
            rgbeT = new long[width * height];
            colTo = new int[width * height];
            energyTo = new long[width * height];
            isTransposed = false;

            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    int v = row * width + col;
                    int rgb = picture.get(col, row).getRGB();
                    rgbe[v] = (long) rgb << Integer.SIZE;
                }
            }
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    int v = row * width + col;
                    int energy = computeEnergy(col, row);
                    rgbe[v] |= energy;
                }
            }
        } else throw new IllegalArgumentException();
    }

    public Picture picture() {
        if (isTransposed)
            transpose();
        Picture picture = new Picture(width, height);
        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
                int v = row * arrWidth + col;
                int rgb = (int) (rgbe[v] >> Integer.SIZE);
                picture.set(col, row, new Color(rgb));
            }
        }
        return picture;
    }

    public int width() { return isTransposed ? height : width; }

    public int height() { return isTransposed ? width : height; }

    public double energy(int x, int y) {
        if (isValid(x, y)) {
            int v = isTransposed ? x * arrWidth + y : y * arrWidth + x;
            return Math.sqrt((int) rgbe[v]);
        } else throw new IllegalArgumentException();
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
        Arrays.fill(energyTo, 0, width, 0);
        for (int row = 1; row < height; ++row) {
            int fromIndex = row * arrWidth;
            int toIndex = fromIndex + width;
            Arrays.fill(energyTo, fromIndex, toIndex, Long.MAX_VALUE);
        }

        for (int row = 0; row + 1 < height; ++row) {
            for (int col = 1; col + 1 < width; ++col) {
                int v = row * arrWidth + col;
                int weight = (int) rgbe[v];
                for (int i = 0; i < 3; ++i) {
                    int w = (row + 1) * arrWidth + col + i - 1;
                    if (energyTo[w] > energyTo[v] + weight) {
                        energyTo[w] = energyTo[v] + weight;
                        colTo[w] = col;
                    }
                }
                assert Boolean.TRUE;
            }
        }

        long minEnergy = Long.MAX_VALUE;
        int minCol = 0;
        int base = (height - 1) * arrWidth;
        for (int col = 1; col + 1 < width; ++col) {
            int v = base + col;
            long energy = energyTo[v];
            if (energy < minEnergy) {
                minEnergy = energy;
                minCol = col;
            }
        }

        int[] seam = new int[height];
        int col = minCol;
        for (int row = height - 1; row >= 0; --row) {
            seam[row] = col;
            int v = row * arrWidth + col;
            col = colTo[v];
        }
        return seam;
    }

    private void removeSeam(int[] seam) {
        if (width > 1 && isValid(seam)) {
            --width;
            for (int row = 0; row < height; ++row) {
                int col = seam[row];
                int srcPos = row * arrWidth + col + 1;
                int destPos = srcPos - 1;
                int length = width - col;
                System.arraycopy(rgbe, srcPos, rgbe, destPos, length);
            }
            for (int row = 0; row < height; ++row) {
                for (int i = 0; i < 2; ++i) {
                    int col = seam[row] + i - 1;
                    int v = row * arrWidth + col;
                    int energy = computeEnergy(col, row);
                    rgbe[v] &= -1L << Integer.SIZE;
                    rgbe[v] |= energy;
                }
            }
        } else throw new IllegalArgumentException();
    }

    private int computeEnergy(int x, int y) {
        if (x > 0 && y > 0 && x + 1 < width && y + 1 < height) {
            int v = y * arrWidth + x;
            int up = (int) (rgbe[v - arrWidth] >> Integer.SIZE);
            int left = (int) (rgbe[v - 1] >> Integer.SIZE);
            int right = (int) (rgbe[v + 1] >> Integer.SIZE);
            int down = (int) (rgbe[v + arrWidth] >> Integer.SIZE);
            int energy = 0;
            for (int i = 0; i < 3; ++i) {
                int dx = (left & 0xff) - (right & 0xff);
                int dy = (up & 0xff) - (down & 0xff);
                energy += dx * dx + dy * dy;

                up >>= 8;
                left >>= 8;
                right >>= 8;
                down >>= 8;
            }
            return energy;
        } else return 1000 * 1000;
    }

    private void transpose() {
        for (int row = 0; row < height; ++row)
            for (int col = 0; col < width; ++col)
                rgbeT[col * arrHeight + row] = rgbe[row * arrWidth + col];

        long[] tempRgbe = rgbe;
        this.rgbe = rgbeT;
        this.rgbeT = tempRgbe;

        int tempArrWidth = arrWidth;
        arrWidth = arrHeight;
        arrHeight = tempArrWidth;

        int tempWidth = width;
        width = height;
        height = tempWidth;

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
            for (int i = 0; i + 1 < seam.length; ++i) {
                int diff = seam[i] - seam[i + 1];
                if (diff * diff > 1)
                    return false;
            }
            return true;
        } else return false;
    }

}
