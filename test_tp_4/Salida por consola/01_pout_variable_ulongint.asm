.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    c_7_ul_ul DD 7
    c_4294967295_ul_ul DD 4294967295
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_7_ul_ul
    mov x_main, eax
    mov eax, c_4294967295_ul_ul
    mov y_main, eax
    invoke crt_printf, offset fmt_ulongint, x_main
    invoke crt_printf, offset fmt_ulongint, y_main
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
