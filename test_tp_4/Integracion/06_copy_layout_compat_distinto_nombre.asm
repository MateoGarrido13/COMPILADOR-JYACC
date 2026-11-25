.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main_izq DD ?
    d_main_izq DQ ?
    y_main_der DD ?
    e_main_der DQ ?
    a_x_main DD ?
    a_d_main DQ ?
    b_y_main DD ?
    b_e_main DQ ?
    c_7_ul_ul DD 7
    c_2_5 DQ 2.5
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0

.code
start:
    jmp main_inicio
main_inicio:
    mov eax, c_7_ul_ul
    mov a_x_main, eax
    fld c_2_5
    fstp a_d_main
    mov eax, a_x_main
    mov b_y_main, eax
    mov eax, dword ptr [a_d_main+4]
    mov dword ptr [b_e_main+4], eax
    mov eax, dword ptr [a_d_main]
    mov dword ptr [b_e_main], eax
    invoke crt_printf, offset fmt_ulongint, b_y_main
    invoke crt_printf, offset fmt_doublef, b_e_main
    invoke ExitProcess, 0
end start
