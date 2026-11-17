.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main_padre DD ?
    b_main_hija DD ?
    h_padre_a_main DD ?
    h_b_main DD ?
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    c_10_ul_ul DD 10
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_1_ul_ul
    mov h_padre_a_main, eax
    mov eax, c_2_ul_ul
    mov h_b_main, eax
    mov eax, c_10_ul_ul
    mov h_padre_a_main, eax
    invoke crt_printf, offset fmt_ulongint, h_b_main
    invoke crt_printf, offset fmt_ulongint, h_padre_a_main
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
