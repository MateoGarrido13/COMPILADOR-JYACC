.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    z_main DD ?
    c_0_ul_ul DD 0
    c_1_ul_ul DD 1
    c_3_ul_ul DD 3
    @aux6 DD ?
    @aux8 DD ?

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_0_ul_ul
    mov x_main, eax
    mov eax, c_0_ul_ul
    mov y_main, eax
label_1:
    mov eax, c_0_ul_ul
    mov y_main, eax
    mov eax, x_main
    add eax, c_1_ul_ul
    mov @aux6, eax
    mov eax, @aux6
    mov x_main, eax
    mov eax, x_main
    add eax, y_main
    mov @aux8, eax
    mov eax, @aux8
    mov z_main, eax
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
