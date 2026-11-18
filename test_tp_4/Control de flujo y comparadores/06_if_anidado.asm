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
    @aux7 DD ?

.code
start:
    mov eax, c_2_ul_ul
    mov a_main, eax
    mov eax, c_3_ul_ul
    mov b_main, eax
    mov eax, c_1_ul_ul
    mov c_main, eax
    mov eax, a_main
    cmp eax, c_0_ul_ul
    seta al
    movzx eax, al
    mov @aux5, eax
    mov eax, a_main
    cmp eax, c_0_ul_ul
    jbe label_3
    mov eax, b_main
    cmp eax, c_0_ul_ul
    seta al
    movzx eax, al
    mov @aux7, eax
    mov eax, b_main
    cmp eax, c_0_ul_ul
    jbe label_1
    mov eax, c_1_ul_ul
    mov r_main, eax
    jmp label_2
label_1:
    mov eax, c_2_ul_ul
    mov r_main, eax
label_2:
    jmp label_4
label_3:
    mov eax, c_3_ul_ul
    mov r_main, eax
label_4:
    invoke ExitProcess, 0
end start
