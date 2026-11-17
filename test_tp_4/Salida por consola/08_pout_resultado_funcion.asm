.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main_foo DD ?
    x_main DD ?
    c_6_ul_ul DD 6
    c_1_ul_ul DD 1
    @aux7 DD ?
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
    ; (FUNCION, foo, --)
    ; (ret, a:main:foo, --)
    ; (FIN_FUNCION, foo, --)
    mov eax, c_6_ul_ul
    mov x_main, eax
    ; (PAR, a:main:foo, x:main)
    ; (CALL, foo:main, 1)
    invoke crt_printf, offset fmt_ulongint, @aux7
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
