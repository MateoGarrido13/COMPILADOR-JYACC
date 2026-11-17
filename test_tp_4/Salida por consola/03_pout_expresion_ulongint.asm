.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DD ?
    b_main DD ?
    c_main DD ?
    c_10_ul_ul DD 10
    c_3_ul_ul DD 3
    c_2_ul_ul DD 2
    @aux5 DD ?
    @aux6 DD ?
    @aux8 DD ?
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_10_ul_ul
    mov a_main, eax
    mov eax, c_3_ul_ul
    mov b_main, eax
    mov eax, c_2_ul_ul
    mov c_main, eax
    mov eax, b_main
    imul eax, c_main
    mov @aux5, eax
    mov eax, a_main
    add eax, @aux5
    mov @aux6, eax
    invoke crt_printf, offset fmt_ulongint, @aux6
    mov eax, a_main
    sub eax, b_main
    mov @aux8, eax
    invoke crt_printf, offset fmt_ulongint, @aux8
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
