package evaluation;

import entity.GameBoard;

/**
 * Created by amare on 14.01.2016.
 */
public class WebEvaluation {

    private final State s = new State();

    public WebEvaluation(GameBoard b) {
        int p = 0, l = 0;
        for (int i = 0; i < 10; i++) {

            for (int j = 0; j < 10; j++) {
                if (b.board[i][j] == -1) {
                    s.white_q_x[p] = i;
                    s.white_q_y[p++] = j;
                    s.white_bd = XOR(s.white_bd, i, j);
                } else if (b.board[i][j] == 1) {
                    s.black_q_x[l] = i;
                    s.black_q_y[l++] = j;
                    s.black_bd = XOR(s.black_bd, i, j);
                } else if (b.board[i][j] == -1) {
                    s.blocks_bd = XOR(s.blocks_bd, i, j);
                }
            }
        }
    }

    private long[] XOR(long[] bd, int col, int row) {
        if (row < 5) {
            bd[0] ^= (long) 1 << ((row * 10) + col);
        } else {
            bd[1] ^= (long) 1 << (((row - 5) * 10) + col);
        }
        return bd;
    }

    public double Evaluate() {
        int p1 = 0, p2 = 0;
        double own1L0 = 0, own2L0 = 0;
        double own1L1 = 0, own2L1 = 0;
        long board_u, board_l;
        long web_board_u, web_board_l;
        long white_web1_u = 0, white_web1_l = 0, black_web1_u = 0, black_web1_l = 0;
        long res1_l, res1_u, res2_l, res2_u;
        long dirs_l = 0, dirs_u = 0;
        long low_quad = Long.decode("0x07c1f07c1f"), high_quad = Long.decode("0xf83e0f83e0");
        int i;
        int[] black_q_pos = new int[4];
        int[] white_q_pos = new int[4];
        int black_misc;
        int white_misc = black_misc = 0;
        for (i = 0; i < 4; i++) {
            black_q_pos[i] = s.black_q_y[i] * 10 + s.black_q_x[i];
            white_q_pos[i] = s.white_q_y[i] * 10 + s.white_q_x[i];
        }
        board_u = s.white_bd[1] | s.black_bd[1] | s.blocks_bd[1];
        board_l = s.white_bd[0] | s.black_bd[0] | s.blocks_bd[0];
        for (i = 0; i < 4; i++) {

            if ((p1 +=
                gen_web_board_count(white_web1_l, white_web1_u, board_l, board_u, white_q_pos[i]))
                == 0)
                white_misc -= 15;  //white queen is trapped.

            if ((p2 +=
                gen_web_board_count(black_web1_l, black_web1_u, board_l, board_u, black_q_pos[i]))
                == 0)
                black_misc -= 15;  //black queen is trapped.
        }


        for (i = 0; i < 4; i++) {
            //check white queens
            long[] temp = gen_dirs_board(dirs_l, dirs_u, white_q_pos[i]);
            dirs_l = temp[0];
            dirs_u = temp[1];
            res1_l = (white_web1_l & dirs_l);
            res1_u = (white_web1_u & dirs_u);

            if (count_bits(res1_l, res1_u) < 3) {
                res2_l = res1_l & black_web1_l;
                res2_u = res1_u & black_web1_u;
                if (!(((res1_l ^ res2_l) != 0) || ((res1_u ^ res2_u) != 0)))
                    white_misc -= 10;
            }

            //check black queens
            temp = gen_dirs_board(dirs_l, dirs_u, black_q_pos[i]);
            dirs_l = temp[0];
            dirs_u = temp[1];
            res1_l = black_web1_l & dirs_l;
            res1_u = black_web1_u & dirs_u;
            if (count_bits(res1_l, res1_u) < 3) {
                res2_l = res1_l & white_web1_l;
                res2_u = res1_u & white_web1_u;
                if (!(((res1_l ^ res2_l) != 0) || ((res1_u ^ res2_u) != 0)))
                    black_misc -= 10;
            }
        }

        for (i = 0; i < 2; i++) {
            if (((s.white_bd[i]) & (low_quad)) == 0)
                white_misc -= 5;
            else if (((s.black_bd[i]) & (low_quad)) == 0)
                white_misc += 5;  //give bonus points for owning a quadrant

            if (((s.white_bd[i]) & (high_quad)) == 0)
                white_misc -= 5;
            else if (((s.black_bd[i]) & (high_quad)) == 0)
                white_misc += 5;  //give bonus points for owning a quadrant

            if (((s.black_bd[i]) & (low_quad)) == 0)
                black_misc -= 5;
            else if (((s.white_bd[i]) & (low_quad)) == 0)
                black_misc += 5;

            if (((s.black_bd[i]) & (high_quad)) == 0)
                black_misc -= 5;
            else if (((s.white_bd[i]) & (high_quad)) == 0)
                black_misc += 5;
        }

        for (i = 0; i < 100; i++) {
            if (i > 49) {
                if (((board_u) & (((long) 0x1 << (i % 50)))) != 0)
                    continue;
            } else {
                if (((board_l) & (((long) 0x1 << (i % 50)))) != 0)
                    continue;
            }

            web_board_l = web_board_u = 0;
            long[] temp = gen_web_board(web_board_l, web_board_u, board_l, board_u, i);
            web_board_l = temp[0];
            web_board_u = temp[1];


            if (s.turn == 1) //player 1 moves next
            {
                if ((web_board_l & s.white_bd[0]) != 0 || (web_board_u & s.white_bd[1]) != 0) {
                    //printf("Square %d is owned by white - level 0\n", i);
                    ++own1L0;
                    if ((web_board_l & s.black_bd[0]) != 0 || (web_board_u & s.black_bd[1]) != 0)
                        own1L0 -= 0.5;
                } else if ((web_board_l & s.black_bd[0]) != 0
                    || (web_board_u & s.black_bd[1]) != 0) {
                    //printf("Square %d is owned by black - level 0\n", i);
                    ++own2L0;
                } else if ((web_board_l & white_web1_l) != 0 || (web_board_u & white_web1_u) != 0) {
                    //printf("Square %d is owned by white - level 1\n", i);
                    ++own1L1;
                    if ((web_board_l & black_web1_l) != 0 || (web_board_u & black_web1_u) != 0)
                        own1L1 -= 0.5;
                } else if ((web_board_l & black_web1_l) != 0 || (web_board_u & black_web1_u) != 0) {
                    //printf("Square %d is owned by black - level 1\n", i);
                    ++own2L1;
                }
            } else {
                if ((web_board_l & s.black_bd[0]) != 0 || (web_board_u & s.black_bd[1]) != 0) {
                    //printf("Square %d is owned by black - level 0\n", i);
                    ++own2L0;
                    if ((web_board_l & s.white_bd[0]) != 0 || (web_board_u & s.white_bd[1]) != 0)
                        own2L0 -= 0.5;
                } else if ((web_board_l & s.white_bd[0]) != 0
                    || (web_board_u & s.white_bd[1]) != 0) {
                    ++own1L0;
                    //printf("Square %d is owned by white - level 0\n", i);
                } else if ((web_board_l & black_web1_l) != 0 || (web_board_u & black_web1_u) != 0) {
                    //printf("Square %d is owned by black - level 1\n", i);
                    ++own2L1;
                    if ((web_board_l & white_web1_l) != 0 || (web_board_u & white_web1_u) != 0)
                        own2L1 -= 0.5;
                } else if ((web_board_l & white_web1_l) != 0 || (web_board_u & white_web1_u) != 0) {
                    //printf("Square %d is owned by white - level 1\n", i);
                    ++own1L1;
                }
            }
        }
        return (((own1L0 + own1L1) * 1.5 + p1 + white_misc) - (p2 + (own2L0 + own2L1) * 1.5
            + black_misc));
    }

    private long[] gen_web_board(long web_l, long web_u, long board_l, long board_u, int pos) {
        short row, col, fdiag, bdiag;
        short web_row, web_col, web_fdiag, web_bdiag;
        int diag;

        //row web
        if (pos > 49)
            row = GET_ROW(board_u, GET_COL_POS(pos));
        else
            row = GET_ROW(board_l, GET_COL_POS(pos));

        web_row = gen_web_stream_plus(row, GET_ROW_POS(pos), 10);
        //printf("Row for pos %d:", pos);

        if (pos > 49)
            web_u = PUT_ROW(web_u, (short) (pos / 10), web_row);
        else
            web_l = PUT_ROW(web_l, (short) (pos / 10), web_row);


        //col web
        col = GET_COL(board_l, board_u, (short) (pos % 10));
        web_col = gen_web_stream_plus(col, GET_COL_POS(pos), 10);


        web_l = PUT_HALF_COL(web_l, (short) (pos % 10), web_col);
        web_u = PUT_HALF_COL(web_u, (short) (pos % 10), web_col);


        //fdiag web
        diag = GET_FDIAG(pos);
        fdiag = (short) get_forward_diag(board_l, board_u, diag);
        web_fdiag = gen_web_stream_plus(fdiag, GET_FDIAG_POS(pos), GET_FDIAG_LEN(diag));



        long[] temp = put_forward_diag(web_l, web_u, web_fdiag, diag);
        web_l = temp[0];
        web_u = temp[1];


        //bdiag web
        diag = GET_BDIAG(pos);
        bdiag = (short) get_back_diag(board_l, board_u, diag);
        web_bdiag = gen_web_stream_plus(bdiag, GET_BDIAG_POS(pos), GET_BDIAG_LEN(diag));



        temp = put_back_diag(web_l, web_u, web_bdiag, diag);
        web_l = temp[0];
        web_u = temp[1];

        long[] ans = new long[2];
        ans[0] = web_l;
        ans[1] = web_u;
        return ans;
    }

    private int count_bits(long board_l, long board_u) {
        int count = 0;
        int i;

        for (i = 0; i < 64; i++) {
            if ((board_l & 0x1) != 0)
                ++count;
            board_l >>= 1;

            if ((board_u & 0x1) != 0)
                ++count;
            board_u >>= 1;
        }
        return count;
    }

    private long[] gen_dirs_board(long board_l, long board_u, int pos) {
        int row = GET_COL_POS(pos);
        int row_adj = row % 5;
        int pos_adj = pos % 50;
        long board_ptr;

        long final_board_l = board_l, final_board_u = board_u;

    /* Generate top row */
        if (row < 9) { //position is not against top border, generate this row
            if (pos > 39) {
                //use upper board
                board_ptr = final_board_u;
                if (pos == 40) {
                    final_board_u = board_ptr | 0x3;
                } else {
                    final_board_u =
                        board_ptr | (((long) 0x7 << ((pos_adj + 9) % 50)) & ((long) 0x3ff << (
                            ((row_adj + 1) % 5) * 10)));
                }
            } else {
                board_ptr = final_board_l;
                if (pos == 40) {
                    final_board_l = board_ptr | 0x3;
                } else {
                    final_board_l =
                        board_ptr | (((long) 0x7 << ((pos_adj + 9) % 50)) & ((long) 0x3ff << (
                            ((row_adj + 1) % 5) * 10)));
                }
            }
            //The 2nd half of this expressions is a row bitmask that takes care of
            //positions next to the left or right borders, ensuring that the bits
            //placed on the board stay in the row
        }

    /* Generate middle row */
        if (pos < 50) {
            board_ptr = final_board_l;  //otherwise board_ptr is still pointing to board_u}
            if (pos_adj == 0) {
                final_board_l =
                    board_ptr | (long) (0x2); //in bottom left corner of board half, can't shift neg
            } else {
                final_board_l =
                    board_ptr | (((long) 0x5 << (pos_adj - 1)) & ((long) 0x3ff << (row_adj * 10)));
            }
        } else {
            board_ptr = final_board_u;  //otherwise board_ptr is still pointing to board_u
            if (pos_adj == 0) {
                final_board_u =
                    board_ptr | (long) (0x2); //in bottom left corner of board half, can't shift neg
            } else {
                final_board_u =
                    board_ptr | (((long) 0x5 << (pos_adj - 1)) & ((long) 0x3ff << (row_adj * 10)));
            }
        }




    /* Generate bottom row */
        if (row > 0) { //position is not against bottom border, generate this row
            if (pos < 60) {
                board_ptr = final_board_l;  //otherwise board_ptr is still pointing to board_u}
                if (pos_adj == 10)
                    final_board_l = board_ptr
                        | (long) 0x3; //in bottom left corner of board half, can't shift neg
                else
                    final_board_l =
                        board_ptr | (((long) 0x7 << ((pos - 11) % 50)) & ((long) 0x3ff << (
                            ((row - 1) % 5) * 10)));
            } else {
                board_ptr = final_board_u;  //otherwise board_ptr is still pointing to board_u
                if (pos_adj == 10)
                    final_board_u = board_ptr
                        | (long) 0x3; //in bottom left corner of board half, can't shift neg
                else
                    final_board_u =
                        board_ptr | (((long) 0x7 << ((pos - 11) % 50)) & ((long) 0x3ff << (
                            ((row - 1) % 5) * 10)));
            }


        }
        long[] temp = new long[2];
        temp[0] = final_board_l;
        temp[1] = final_board_u;
        return temp;
    }

    private int GET_COL_POS(int pos) {
        return pos / 10;
    }

    private int GET_ROW_POS(int pos) {
        return pos % 10;
    }

    private long PUT_ROW(long board, short row, short stream) {
        return board | ((long) stream << ((row % 5) * 10));
    }

    private int gen_web_board_count(long web_l, long web_u, long board_l, long board_u, int pos) {
        short row, col, fdiag, bdiag;
        short web_row, web_col, web_fdiag, web_bdiag;
        int diag;
        int row_count, col_count, fdiag_count, bdiag_count;

        if (pos > 49)
            row = GET_ROW(board_u, GET_COL_POS(pos));
        else
            row = GET_ROW(board_l, GET_COL_POS(pos));

        web_row = gen_web_stream(row, GET_ROW_POS(pos), 10);
        row_count = count_contig_bits(web_row, 10);

        if (pos > 49)
            web_u = PUT_ROW(web_u, (short) (pos / 10), web_row);
        else
            web_l = PUT_ROW(web_l, (short) (pos / 10), web_row);
        col = GET_COL(board_l, board_u, (short) (pos % 10));

        web_col = gen_web_stream(col, GET_COL_POS(pos), 10);
        col_count = count_contig_bits(web_col, 10);

        web_l = PUT_HALF_COL(web_l, (short) (pos % 10), web_col);
        web_u = PUT_HALF_COL(web_u, (short) (pos % 10), web_col >> 5);

        diag = GET_FDIAG(pos);
        fdiag = (short) get_forward_diag(board_l, board_u, diag);
        web_fdiag = gen_web_stream(fdiag, GET_FDIAG_POS(pos), GET_FDIAG_LEN(diag));
        fdiag_count = count_contig_bits(web_fdiag, GET_FDIAG_LEN(diag));

        long[] temp = put_forward_diag(web_l, web_u, web_fdiag, diag);
        web_l = temp[0];
        web_u = temp[1];

        diag = GET_BDIAG(pos);
        bdiag = (short) get_back_diag(board_l, board_u, diag);
        web_bdiag = gen_web_stream(bdiag, GET_BDIAG_POS(pos), GET_BDIAG_LEN(diag));
        bdiag_count = count_contig_bits(web_bdiag, GET_BDIAG_LEN(diag));

        return (row_count + col_count + fdiag_count + bdiag_count - 4);
    }

    private long[] put_back_diag(long board_l, long board_u, short stream, int diag) {
        int len = GET_BDIAG_LEN(diag);
        int i;
        int pos = diag;
        short mask = 0x1;

        long final_board_l = board_l, final_board_u = board_u;
        long board;

        for (i = 0; i < len; i++) {
            //This needs some funky footwork to get bits in the early 50's
            if (pos + i > 49) {
                if (pos > 49) {
                    board = final_board_u;
                    final_board_u = board | ((long) (stream & mask) << (pos % 50));
                } else {
                    board = final_board_l;
                    final_board_l = board | ((long) (stream & mask) >> (50 - pos));
                }
            } else {
                board = final_board_l;
                final_board_l = board | ((long) (stream & mask) << pos);
            }

            pos += 8;
            mask <<= 1;
        }
        long[] temp = new long[2];
        temp[0] = final_board_l;
        temp[1] = final_board_u;
        return temp;
    }

    private int GET_BDIAG_POS(int b) {
        return (b / 10 < (10 - b % 10)) ? b / 10 : 9 - b % 10;
    }

    private int get_back_diag(long board_l, long board_u, int diag) {
        int len = GET_BDIAG_LEN(diag);
        int i;
        short res = 0;
        int pos = diag;
        short mask = 0x1;

        for (i = 0; i <= len; i++) {
            //This needs some funky footwork to get bits in the early 50's
            if (pos + i > 49) {
                if (pos > 49) {
                    res |= (board_u >> (pos % 50)) & mask;
                } else {
                    res |= (board_u << (50 - pos)) & mask;
                }
            } else
                res |= (board_l >> pos) & mask;

            pos += 8;
            mask <<= 1;
        }

        return res;
    }

    private int GET_BDIAG_LEN(int bdiag) {
        return (bdiag < 10) ? bdiag + 1 : (10 - bdiag / 10);
    }

    private int GET_BDIAG(int b) {
        return (b / 10 < (10 - b % 10)) ? b - ((b / 10) * 9) : b - ((9 - (b % 10)) * 9);
    }

    private long[] put_forward_diag(long board_l, long board_u, short stream, int diag) {
        int len = GET_FDIAG_LEN(diag);
        int i;
        long board;
        int pos = diag;
        short mask = 0x1;

        long final_board_l = board_l, final_board_u = board_u;

        for (i = 0; i < len; i++) {
            if (pos > 49) {
                board = final_board_u;
                final_board_u = board | ((long) (stream & mask) << (pos % 50));
            } else {
                board = final_board_l;
                final_board_l = board | ((long) (stream & mask) << (pos % 50));
            }

            pos += 10;
            mask <<= 1;
        }
        long[] ans = new long[2];
        ans[0] = final_board_l;
        ans[1] = final_board_u;
        return ans;
    }

    private int GET_FDIAG_POS(int f) {
        return ((f % 10 > f / 10) ? f / 10 : f % 10);
    }

    private int get_forward_diag(long board_l, long board_u, int diag) {
        int len = GET_FDIAG_LEN(diag);
        int i;
        short res = 0;
        long board;
        int pos = diag;
        short mask = 0x1;

        for (i = 0; i < len; i++) {
            if (pos > 49)
                board = board_u;
            else
                board = board_l;

            res |= (board >> (pos % 50)) & mask;
            pos += 10;
            mask <<= 1;
        }

        return res;
    }

    private int GET_FDIAG_LEN(int fdiag) {
        return (fdiag < 10) ? (10 - fdiag) : (10 - fdiag / 10);
    }

    private int GET_FDIAG(int f) {
        return (f % 10 > f / 10) ? f - ((f / 10) * 11) : f - ((f % 10) * 11);
    }

    private long PUT_HALF_COL(long board, short col, long stream) {
        return board | (((stream & 0x1) << col) | ((stream & 0x2) << (col + 9)) | ((stream & 0x4)
            << (col + 18)) | ((stream & 0x8) << (col + 27)) | ((stream & 0x10) << (col + 36)));
    }

    private short gen_web_stream_plus(short stream, int pos, int len) {
        short web = 0;
        int i;

        web |= 0x1 << pos;

        for (i = pos - 1; i >= 0; i--) {
            if ((stream & (0x1 << i)) != 0) {
                web |= 0x1 << i;
                break;
            } else
                web |= 0x1 << i;
        }

        for (i = pos + 1; i < len; i++) {
            if ((stream & (0x1 << i)) != 0) {
                web |= 0x1 << i;
                break;
            } else
                web |= 0x1 << i;
        }

        return web;
    }

    private short GET_COL(long board_l, long board_u, short col) {
        return (short) (GET_HALF_COL(board_l, col) | (GET_HALF_COL(board_u, col) << 5));
    }

    private long GET_HALF_COL(long board, short col) {
        return (((board >> col) & 0x1) | ((board >> (col + 9)) & 0x2) | ((board >> (col + 18))
            & 0x4) | ((board >> (col + 27)) & 0x8) | ((board >> (col + 36)) & 0x10));
    }

    private int count_contig_bits(short stream, int len) {
        int i;
        int count = 0;

        for (i = 0; i < len; i++) {
            if ((stream & (0x1 << i)) != 0)
                ++count;
            else if (count != 0)
                return count;
        }

        return count;
    }

    private short gen_web_stream(short stream, int pos, int len) {
        short web = 0;
        int i;

        web |= 0x1 << pos;

        for (i = pos - 1; i >= 0; i--) {
            if ((stream & (0x1 << i)) != 0)
                break;
            else
                web |= 0x1 << i;
        }

        for (i = pos + 1; i < len; i++) {
            if ((stream & (0x1 << i)) != 0)
                break;
            else
                web |= 0x1 << i;
        }

        return web;
    }

    private short GET_ROW(long board, int row) {
        return (short) ((board >> (row % 5) * 10) & 0x3ff);
    }

    private static class State {

        final int[] white_q_x = new int[4];
        final int[] white_q_y = new int[4];
        final int[] black_q_x = new int[4];
        final int[] black_q_y = new int[4];
        final char turn;
        final short value;
        final char depth;
        final char winner;
        public long[] white_bd = new long[2];
        public long[] black_bd = new long[2];
        public long[] blocks_bd = new long[2];

        public State() {
            winner = 0;
            turn = 1;
            blocks_bd[0] = blocks_bd[1] = 0;
            white_bd[0] = white_bd[1] = 0;
            black_bd[0] = black_bd[1] = 0;
            for (int i = 0; i < 4; i++) {

                white_q_x[i] = 0;
                white_q_y[i] = 0;
                black_q_x[i] = 0;
                black_q_y[i] = 0;
            }
            depth = 0;
            value = 0;
        }
    }
}
