.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    z_main DQ ?
    c_2_5 DQ 2.5
    c_1_0 DQ 1.0
    @aux4 DQ ?
    fmt_doublef DB "%f",0

.code
start:
    ; (PROGRAMA, --, --)
    fld c_2_5
    fstp z_main
    invoke crt_printf, offset fmt_doublef, z_main
    fld z_main
    fadd c_1_0
    fstp @aux4
    invoke crt_printf, offset fmt_doublef, @aux4
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
