.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DQ ?
    y_main DQ ?
    r_main DQ ?
    c_1_7976931348623157d_pos_308 DQ 1.7976931348623157d+308
    c_2_0 DQ 2.0
    @aux4 DQ ?
    fmt_doublef DB "%f",0
    msg_overflow_float DB "Error: overflow en producto de doublef",0

.code
start:
    fld c_1_7976931348623157d_pos_308
    fstp x_main
    fld c_2_0
    fstp y_main
    fld x_main
    fmul y_main
    fstp @aux4
    fld @aux4
    fxam
    fstsw ax
    fstp st(0)
    sahf
    jc error_overflow_float
    fld @aux4
    fstp r_main
    invoke crt_printf, offset fmt_doublef, r_main
    jmp fin_normal
error_overflow_float:
    invoke crt_printf, offset msg_overflow_float
    invoke ExitProcess, 2
fin_normal:
    invoke ExitProcess, 0
end start
