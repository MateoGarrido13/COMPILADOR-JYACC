.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main_eco DD ?
    dentro DB "dentro",0
    x_main DD ?
    c_2_ul_ul DD 2
    c_1_ul_ul DD 1
    @aux9 DD ?
    fmt_ulongint DB "%u",0
    fmt_cadena DB "%s",0

.code
start:
    ; (PROGRAMA, --, --)
    ; (FUNCION, eco, --)
    invoke crt_printf, offset fmt_cadena, offset dentro
    invoke crt_printf, offset fmt_ulongint, a_main_eco
    ; (ret, a:main:eco, --)
    ; (FIN_FUNCION, eco, --)
    mov eax, c_2_ul_ul
    mov x_main, eax
    ; (PAR, a:main:eco, x:main)
    ; (CALL, eco:main, 1)
    invoke crt_printf, offset fmt_ulongint, @aux9
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
