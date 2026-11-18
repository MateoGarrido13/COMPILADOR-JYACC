.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    c_9_ul_ul DD 9
    @aux3 DQ ?
    fmt_doublef DB "%f",0

.code
start:
    mov eax, c_9_ul_ul
    mov x_main, eax
    fild x_main
    fstp @aux3
    invoke crt_printf, offset fmt_doublef, @aux3
    invoke ExitProcess, 0
end start
