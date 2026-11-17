.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main_punto DD ?
    y_main_punto DD ?
    p1_x_main DD ?
    p1_y_main DD ?
    p2_x_main DD ?
    p2_y_main DD ?
    c_10_ul_ul DD 10
    c_20_ul_ul DD 20
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_10_ul_ul
    mov p1_x_main, eax
    mov eax, c_20_ul_ul
    mov p1_y_main, eax
    mov eax, p1_x_main
    mov p2_x_main, eax
    mov eax, p1_y_main
    mov p2_y_main, eax
    invoke crt_printf, offset fmt_ulongint, p2_x_main
    invoke crt_printf, offset fmt_ulongint, p2_y_main
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
