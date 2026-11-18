.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DQ ?
    b_main DQ ?
    r_main DQ ?
    c_1_7976931348623157d_pos_308 DQ 1.7976931348623157d+308
    @aux4 DQ ?
    fmt_doublef DB "%f",0
    msg_overflow_float DB "Error: overflow en producto de doublef",0

.code
start:
    fld c_1_7976931348623157d_pos_308
    fstp a_main
    fld c_1_7976931348623157d_pos_308
    fstp b_main
    fld a_main
    fmul b_main
    fstp @aux4
    fld @aux4
    fxam
    fstsw ax
    sahf
    jc error_overflow_float
    fld @aux4
    fstp r_main
    invoke crt_printf, offset fmt_doublef, r_main
error_overflow_float:
    invoke crt_printf, offset msg_overflow_float
    invoke ExitProcess, 2
    invoke ExitProcess, 0
end start
