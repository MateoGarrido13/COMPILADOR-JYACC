.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    z_main DQ ?
    c_4_ul_ul DD 4
    c_1_25 DQ 1.25
    resultado DB "resultado",0
    c_1_ul_ul DD 1
    @aux7 DD ?
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0
    fmt_cadena DB "%s",0

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_4_ul_ul
    mov x_main, eax
    fld c_1_25
    fstp z_main
    invoke crt_printf, offset fmt_cadena, offset resultado
    invoke crt_printf, offset fmt_ulongint, x_main
    invoke crt_printf, offset fmt_doublef, z_main
    mov eax, x_main
    add eax, c_1_ul_ul
    mov @aux7, eax
    invoke crt_printf, offset fmt_ulongint, @aux7
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
