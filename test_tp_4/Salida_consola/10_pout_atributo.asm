.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main_punto DD ?
    p1_x_main DD ?
    c_8_ul_ul DD 8
    fmt_ulongint DB "%u",0

.code
start:
    jmp main_inicio
main_inicio:
    mov eax, c_8_ul_ul
    mov p1_x_main, eax
    invoke crt_printf, offset fmt_ulongint, p1_x_main
    invoke ExitProcess, 0
end start
