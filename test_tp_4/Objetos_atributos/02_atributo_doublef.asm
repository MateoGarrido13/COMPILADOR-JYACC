.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main_punto DD ?
    y_main_punto DQ ?
    p1_x_main DD ?
    p1_y_main DQ ?
    c_5_ul_ul DD 5
    c_3_5 DQ 3.5
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0

.code
start:
    jmp main_inicio
main_inicio:
    mov eax, c_5_ul_ul
    mov p1_x_main, eax
    fld c_3_5
    fstp p1_y_main
    invoke crt_printf, offset fmt_ulongint, p1_x_main
    invoke crt_printf, offset fmt_doublef, p1_y_main
    invoke ExitProcess, 0
end start
