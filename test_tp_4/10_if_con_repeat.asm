.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    flag_main DD ?
    x_main DD ?
    s_main DD ?
    c_1_ul_ul DD 1
    c_0_ul_ul DD 0
    c_5_ul_ul DD 5
    c_99_ul_ul DD 99
    @aux5 DD ?
    @aux8 DD ?

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_1_ul_ul
    mov flag_main, eax
    mov eax, c_0_ul_ul
    mov x_main, eax
    mov eax, c_0_ul_ul
    mov s_main, eax
    mov eax, flag_main
    cmp eax, c_0_ul_ul
    seta al
    movzx eax, al
    mov @aux5, eax
    mov eax, flag_main
    cmp eax, c_0_ul_ul
    jbe label_2
label_1:
    mov eax, s_main
    add eax, x_main
    mov @aux8, eax
    mov eax, @aux8
    mov s_main, eax
label_2:
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
