.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main_base DD ?
    y_main_derivada DD ?
    d1_base_x_main DD ?
    d1_y_main DD ?
    d2_base_x_main DD ?
    d2_y_main DD ?
    c_100_ul_ul DD 100
    c_200_ul_ul DD 200
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_100_ul_ul
    mov d1_base_x_main, eax
    mov eax, c_200_ul_ul
    mov d1_y_main, eax
    mov eax, d1_base_x_main
    mov d2_base_x_main, eax
    mov eax, d1_y_main
    mov d2_y_main, eax
    invoke crt_printf, offset fmt_ulongint, d2_base_x_main
    invoke crt_printf, offset fmt_ulongint, d2_y_main
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
