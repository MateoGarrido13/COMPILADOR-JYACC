.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DD ?
    b_main DD ?
    c_main DD ?
    r_main DD ?
    c_2_ul_ul DD 2
    c_3_ul_ul DD 3
    c_1_ul_ul DD 1
    c_0_ul_ul DD 0
    @aux5 DD ?
    @aux6 DD ?
    @aux7 DD ?

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_2_ul_ul
    mov a_main, eax
    mov eax, c_3_ul_ul
    mov b_main, eax
    mov eax, c_1_ul_ul
    mov c_main, eax
    mov eax, a_main
    add eax, b_main
    mov @aux5, eax
    mov eax, c_main
    imul eax, c_2_ul_ul
    mov @aux6, eax
    mov eax, @aux5
    cmp eax, @aux6
    seta al
    movzx eax, al
    mov @aux7, eax
    mov eax, @aux5
    cmp eax, @aux6
    jbe label_1
    mov eax, c_1_ul_ul
    mov r_main, eax
    jmp label_2
label_1:
    mov eax, c_0_ul_ul
    mov r_main, eax
label_2:
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
