.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    c_5_ul_ul DD 5
    c_10_ul_ul DD 10
    @aux2 DD ?
    fmt_ulongint DB "%u",0

.code
start:
    mov eax, c_5_ul_ul
    sub eax, c_10_ul_ul
    mov @aux2, eax
    mov eax, @aux2
    mov x_main, eax
    invoke crt_printf, offset fmt_ulongint, x_main
    invoke ExitProcess, 0
end start
