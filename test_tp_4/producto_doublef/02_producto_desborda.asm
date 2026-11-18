.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DQ ?
    b_main DQ ?
    r_main DQ ?
    c_1000000_0 DQ 1000000.0
    c_2000000_0 DQ 2000000.0
    @aux4 DQ ?
    fmt_doublef DB "%f",0
    msg_overflow_float DB "Error: overflow en producto de doublef",0

.code
start:
    fld c_1000000_0
    fstp a_main
    fld c_2000000_0
    fstp b_main
    fld a_main
    fmul b_main
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
