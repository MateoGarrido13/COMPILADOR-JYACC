.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DD ?
    b_main DD ?
    k_main DD ?
    c_5_ul_ul DD 5
    c_3_ul_ul DD 3
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    @aux4 DD ?

.code
start:
    mov eax, c_5_ul_ul
    mov a_main, eax
    mov eax, c_3_ul_ul
    mov b_main, eax
    mov eax, a_main
    cmp eax, b_main
    seta al
    movzx eax, al
    mov @aux4, eax
    mov eax, a_main
    cmp eax, b_main
    jbe label_1
    mov eax, c_1_ul_ul
    mov k_main, eax
    jmp label_2
label_1:
    mov eax, c_2_ul_ul
    mov k_main, eax
label_2:
    invoke ExitProcess, 0
end start
