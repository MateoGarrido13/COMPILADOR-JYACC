.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    c_10_ul_ul DD 10
    c_20_ul_ul DD 20
    fmt_ulongint DB "%u",0

.code
start:
    mov eax, c_10_ul_ul
    mov x_main, eax
    mov eax, c_20_ul_ul
    mov y_main, eax
    invoke crt_printf, offset fmt_ulongint, x_main
    invoke crt_printf, offset fmt_ulongint, y_main
    invoke ExitProcess, 0
end start
