package com.example.cubesolver;

class Util {
    //Moves
    static final byte Ux1 = 0;      // Turn 90 degrees clockwise
    static final byte Ux2 = 1;      // Turn 180 degrees clockwise
    static final byte Ux3 = 2;      // Turn 90 degrees counterclockwise
    static final byte Rx1 = 3;
    static final byte Rx2 = 4;
    static final byte Rx3 = 5;
    static final byte Fx1 = 6;
    static final byte Fx2 = 7;
    static final byte Fx3 = 8;
    static final byte Dx1 = 9;
    static final byte Dx2 = 10;
    static final byte Dx3 = 11;
    static final byte Lx1 = 12;
    static final byte Lx2 = 13;
    static final byte Lx3 = 14;
    static final byte Bx1 = 15;
    static final byte Bx2 = 16;
    static final byte Bx3 = 17;

    /* Facelet mapping
        |1|2|3|
        |4|5|6|
        |7|8|9|
    */
    static final byte U1 = 0;
    static final byte U2 = 1;
    static final byte U3 = 2;
    static final byte U4 = 3;
    static final byte U5 = 4;
    static final byte U6 = 5;
    static final byte U7 = 6;
    static final byte U8 = 7;
    static final byte U9 = 8;
    static final byte R1 = 9;
    static final byte R2 = 10;
    static final byte R3 = 11;
    static final byte R4 = 12;
    static final byte R5 = 13;
    static final byte R6 = 14;
    static final byte R7 = 15;
    static final byte R8 = 16;
    static final byte R9 = 17;
    static final byte F1 = 18;
    static final byte F2 = 19;
    static final byte F3 = 20;
    static final byte F4 = 21;
    static final byte F5 = 22;
    static final byte F6 = 23;
    static final byte F7 = 24;
    static final byte F8 = 25;
    static final byte F9 = 26;
    static final byte D1 = 27;
    static final byte D2 = 28;
    static final byte D3 = 29;
    static final byte D4 = 30;
    static final byte D5 = 31;
    static final byte D6 = 32;
    static final byte D7 = 33;
    static final byte D8 = 34;
    static final byte D9 = 35;
    static final byte L1 = 36;
    static final byte L2 = 37;
    static final byte L3 = 38;
    static final byte L4 = 39;
    static final byte L5 = 40;
    static final byte L6 = 41;
    static final byte L7 = 42;
    static final byte L8 = 43;
    static final byte L9 = 44;
    static final byte B1 = 45;
    static final byte B2 = 46;
    static final byte B3 = 47;
    static final byte B4 = 48;
    static final byte B5 = 49;
    static final byte B6 = 50;
    static final byte B7 = 51;
    static final byte B8 = 52;
    static final byte B9 = 53;

    //Colors
    static final byte U = 0;    // Up
    static final byte R = 1;    // Right
    static final byte F = 2;    // Front
    static final byte D = 3;    // Down
    static final byte L = 4;    // Left
    static final byte B = 5;    // Back


    // Use facelet to represent the corner blocks (8 corner blocks)
    static final byte[][] cornerFacelet = {
            {U9, R1, F3}, {U7, F1, L3}, {U1, L1, B3}, {U3, B1, R3},
            {D3, F9, R7}, {D1, L9, F7}, {D7, B9, L7}, {D9, R9, B7}
    };
    // Use facelet to represent the edge blocks (12 edge blocks)
    static final byte[][] edgeFacelet = {
            {U6, R2}, {U8, F2}, {U4, L2}, {U2, B2}, {D6, R8}, {D2, F8},
            {D4, L8}, {D8, B8}, {F6, R4}, {F4, L6}, {B6, L4}, {B4, R6}
    };

    // Calculate the combination of n and k
    /*
    * Cnk 数组被用于 getComb 和 setComb 方法，这些方法分别用于获取和设置特定排列组合在数组中的索引。这对于将魔方的状态编码为整数并在搜索解决方案时处理排列组合非常有用。
    * */
    static int[][] Cnk = new int[13][13];
    static String[] move2str = {
            "U ", "U2", "U'", "R ", "R2", "R'", "F ", "F2", "F'",
            "D ", "D2", "D'", "L ", "L2", "L'", "B ", "B2", "B'"
    };
    static int[] ud2std = {Ux1, Ux2, Ux3, Rx2, Fx2, Dx1, Dx2, Dx3, Lx2, Bx2, Rx1, Rx3, Fx1, Fx3, Lx1, Lx3, Bx1, Bx3};
    static int[] std2ud = new int[18];  //从标准魔方移动（std）到优化魔方移动（ud）的映射。
    static int[] ckmv2bit = new int[11];    // (check move) 存储魔方移动的冲突信息。ckmv2bit[i] 的每个位表示在执行第 i 个优化移动之后是否可以立即执行第 j 个优化移动。如果位为1，表示不允许立即执行；位为0，表示允许立即执行。

    static class Solution {
        int length = 0; // number of moves
        int depth1 = 0; // IDA*算法中阶段1的解决方案长度。
        int verbose = 0; // 0: no output, 1: output solution, 2: output solution and scramble
        int urfIdx = 0; // 0:URF, 1:UR, 2:UF, 3:UFR, 4:ULB, 5:Min2Phase
        int[] moves = new int[31];  // moves

        Solution() {
        }

        // Set the arguments for the solution
        void setArgs(int verbose, int urfIdx, int depth1) {
            this.verbose = verbose;
            this.urfIdx = urfIdx;
            this.depth1 = depth1;
        }

        // 将给定的移动添加到解决方案中，并合并连续的相同轴上的移动
        void appendSolMove(int curMove) {
            if (length == 0) {
                moves[length++] = curMove;
                return;
            }
            int axisCur = curMove / 3;
            int axisLast = moves[length - 1] / 3;
            if (axisCur == axisLast) {
                int pow = (curMove % 3 + moves[length - 1] % 3 + 1) % 4;
                if (pow == 3) {
                    length--;
                } else {
                    moves[length - 1] = axisCur * 3 + pow;
                }
                return;
            }
            if (length > 1
                    && axisCur % 3 == axisLast % 3
                    && axisCur == moves[length - 2] / 3) {
                int pow = (curMove % 3 + moves[length - 2] % 3 + 1) % 4;
                if (pow == 3) {
                    moves[length - 2] = moves[length - 1];
                    length--;
                } else {
                    moves[length - 2] = axisCur * 3 + pow;
                }
                return;
            }
            moves[length++] = curMove;
        }

        // 生成解决方案的字符串表示
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            int urf = (verbose & Solver.INVERSE_SOLUTION) != 0 ? (urfIdx + 3) % 6 : urfIdx;
            if (urf < 3) {
                for (int s = 0; s < length; s++) {
                    if ((verbose & Solver.USE_SEPARATOR) != 0 && s == depth1) {
                        stringBuffer.append(".  ");
                    }
                    stringBuffer.append(move2str[Cubie.urfMove[urf][moves[s]]]).append(' ');
                }
            } else {
                for (int s = length - 1; s >= 0; s--) {
                    stringBuffer.append(move2str[Cubie.urfMove[urf][moves[s]]]).append(' ');
                    if ((verbose & Solver.USE_SEPARATOR) != 0 && s == depth1) {
                        stringBuffer.append(".  ");
                    }
                }
            }
            if ((verbose & Solver.APPEND_LENGTH) != 0) {
                stringBuffer.append("(").append(length).append("f)");
            }
            return stringBuffer.toString();
        }
    }

    // Cubie 用每个块的位置和方向表示魔方的状态。
    static void toCubieCube(byte[] faces, Cubie ccRet) {
        byte ori;   //orientation of cubie
        for (int i = 0; i < 8; i++) {
            ccRet.ca[i] = 0;
        }
        for (int i = 0; i < 12; i++) {
            ccRet.ea[i] = 0;
        }
        byte col1, col2;
        for (byte i = 0; i < 8; i++) {
            for (ori = 0; ori < 3; ori++) {
                if (faces[cornerFacelet[i][ori]] == U || faces[cornerFacelet[i][ori]] == D)
                    break;
            }
            col1 = faces[cornerFacelet[i][(ori + 1) % 3]];
            col2 = faces[cornerFacelet[i][(ori + 2) % 3]];
            for (byte j = 0; j < 8; j++) {
                if (col1 == cornerFacelet[j][1] / 9 && col2 == cornerFacelet[j][2] / 9) {
                    ccRet.ca[i] = (byte) (ori % 3 << 3 | j);
                    break;
                }
            }
        }
        for (byte i = 0; i < 12; i++) {
            for (byte j = 0; j < 12; j++) {
                if (faces[edgeFacelet[i][0]] == edgeFacelet[j][0] / 9
                        && faces[edgeFacelet[i][1]] == edgeFacelet[j][1] / 9) {
                    ccRet.ea[i] = (byte) (j << 1);
                    break;
                }
                if (faces[edgeFacelet[i][0]] == edgeFacelet[j][1] / 9
                        && faces[edgeFacelet[i][1]] == edgeFacelet[j][0] / 9) {
                    ccRet.ea[i] = (byte) (j << 1 | 1);
                    break;
                }
            }
        }
    }

    // FaceCube 通过每个面的颜色来表示魔方的状态。
    static String toFaceCube(Cubie cc) {
        char[] faceCube = new char[54];
        char[] ts = {'U', 'R', 'F', 'D', 'L', 'B'};
        for (int i = 0; i < 54; i++) {
            faceCube[i] = ts[i / 9];
        }
        for (byte c = 0; c < 8; c++) {
            int j = cc.ca[c] & 0x7;
            int ori = cc.ca[c] >> 3;
            for (byte n = 0; n < 3; n++) {
                faceCube[cornerFacelet[c][(n + ori) % 3]] = ts[cornerFacelet[j][n] / 9];
            }
        }
        for (byte e = 0; e < 12; e++) {
            int j = cc.ea[e] >> 1;
            int ori = cc.ea[e] & 1;
            for (byte n = 0; n < 2; n++) {
                faceCube[edgeFacelet[e][(n + ori) % 2]] = ts[edgeFacelet[j][n] / 9];
            }
        }
        return new String(faceCube);
    }

    /*
    * 计算魔方的排列奇偶性。
    * 在魔方求解中，奇偶性是一个重要的概念，它可以帮助我们了解魔方的状态是否有效，以及在求解过程中可能需要的步骤。
    * 例如，如果魔方的排列奇偶性为偶数，则魔方的状态是有效的，否则它是无效的。
    * idx，它表示我们要计算哪个部分的奇偶性。当idx等于0时，计算角块的排列奇偶性；当idx等于4时，计算棱块的排列奇偶性。
    */
    static int getNParity(int idx, int n) {
        int p = 0;
        for (int i = n - 2; i >= 0; i--) {
            p ^= idx % (n - i);
            idx /= (n - i);
        }
        return p & 1;
    }

    static byte setVal(int val0, int val, boolean isEdge) {
        return (byte) (isEdge ? (val << 1 | val0 & 1) : (val | val0 & ~7));
    }

    static int getVal(int val0, boolean isEdge) {
        return isEdge ? val0 >> 1 : val0 & 7;
    }

    // setNPerm 和 getNPerm，用于处理魔方边缘（edge）和角落（corner）的置换（permutation）。
    static void setNPerm(byte[] arr, int idx, int n, boolean isEdge) {
        long val = 0xFEDCBA9876543210L;
        long extract = 0;
        for (int p = 2; p <= n; p++) {
            extract = extract << 4 | idx % p;
            idx /= p;
        }
        for (int i = 0; i < n - 1; i++) {
            int v = ((int) extract & 0xf) << 2;
            extract >>= 4;
            arr[i] = setVal(arr[i], (int) (val >> v & 0xf), isEdge);
            long m = (1L << v) - 1;
            val = val & m | val >> 4 & ~m;
        }
        arr[n - 1] = setVal(arr[n - 1], (int) (val & 0xf), isEdge);
    }

    static int getNPerm(byte[] arr, int n, boolean isEdge) {
        int idx = 0;
        long val = 0xFEDCBA9876543210L;
        for (int i = 0; i < n - 1; i++) {
            int v = getVal(arr[i], isEdge) << 2;
            idx = (n - i) * idx + (int) (val >> v & 0xf);
            val -= 0x1111111111111110L << v;
        }
        return idx;
    }

    // getComb 和 setComb，用于处理魔方边缘（edge）和角落（corner）的组合（combination）。
    static int getComb(byte[] arr, int mask, boolean isEdge) {
        int end = arr.length - 1;
        int idxC = 0, r = 4;
        for (int i = end; i >= 0; i--) {
            int perm = getVal(arr[i], isEdge);
            if ((perm & 0xc) == mask) {
                idxC += Cnk[i][r--];
            }
        }
        return idxC;
    }

    static void setComb(byte[] arr, int idxC, int mask, boolean isEdge) {
        int end = arr.length - 1;
        int r = 4, fill = end;
        for (int i = end; i >= 0; i--) {
            if (idxC >= Cnk[i][r]) {
                idxC -= Cnk[i][r--];
                arr[i] = setVal(arr[i], r | mask, isEdge);
            } else {
                if ((fill & 0xc) == mask) {
                    fill -= 4;
                }
                arr[i] = setVal(arr[i], fill--, isEdge);
            }
        }
    }

    /**
     * Check whether the cube definition string s represents a solvable cube.
     *
     * @param facelets is the cube definition string
     * @return 0: Cube is solvable<br>
     *         -1: There is not exactly one facelet of each colour
     *         -2: Not all 12 edges exist exactly once
     *         -3: Flip error: One edge has to be flipped
     *         -4: Not all 8 corners exist exactly once
     *         -5: Twist error: One corner has to be twisted
     *         -6: Parity error: Two corners or two edges have to be exchanged
     */
    public static int verify(String facelets) {
        return new Solver().verify(facelets);
    }

    static {
        for (int i = 0; i < 18; i++) {
            std2ud[ud2std[i]] = i;
        }
        for (int i = 0; i < 10; i++) {
            int ix = ud2std[i] / 3;
            ckmv2bit[i] = 0;
            for (int j = 0; j < 10; j++) {
                int jx = ud2std[j] / 3;
                ckmv2bit[i] |= ((ix == jx) || ((ix % 3 == jx % 3) && (ix >= jx)) ? 1 : 0) << j;
            }
        }
        ckmv2bit[10] = 0;
        // 使用 Pascal's triangle（帕斯卡三角形）方法计算并填充 Cnk 数组。
        for (int i = 0; i < 13; i++) {
            Cnk[i][0] = Cnk[i][i] = 1;
            for (int j = 1; j < i; j++) {
                Cnk[i][j] = Cnk[i - 1][j - 1] + Cnk[i - 1][j];
            }
        }
    }
}

