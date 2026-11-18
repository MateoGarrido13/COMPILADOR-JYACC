.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DD ?
    b_main DD ?
    k_main DD ?
    x_main DD ?
    c_10_ul_ul DD 10
    c_5_ul_ul DD 5
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    c_3_ul_ul DD 3
    c_4_ul_ul DD 4
    c_6_ul_ul DD 6
    @aux4 DD ?
    @aux8 DD ?
    @aux12 DD ?
    @aux16 DD ?
    @aux20 DD ?
    @aux24 DD ?

.code
start:
    mov eax, c_10_ul_ul
    mov a_main, eax
    mov eax, c_5_ul_ul
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
label_1:
    mov eax, a_main
    cmp eax, b_main
    setb al
    movzx eax, al
    mov @aux8, eax
    mov eax, a_main
    cmp eax, b_main
    jae label_2
    mov eax, c_2_ul_ul
    mov k_main, eax
label_2:
    mov eax, a_main
    cmp eax, b_main
    setae al
    movzx eax, al
    mov @aux12, eax
    mov eax, a_main
    cmp eax, b_main
    jb label_3
    mov eax, c_3_ul_ul
    mov k_main, eax
label_3:
    mov eax, a_main
    cmp eax, b_main
    setbe al
    movzx eax, al
    mov @aux16, eax
    mov eax, a_main
    cmp eax, b_main
    ja label_4
    mov eax, c_4_ul_ul
    mov k_main, eax
label_4:
    mov eax, a_main
    cmp eax, b_main
    sete al
    movzx eax, al
    mov @aux20, eax
    mov eax, a_main
    cmp eax, b_main
    jne label_5
    mov eax, c_5_ul_ul
    mov k_main, eax
label_5:
    mov eax, a_main
    cmp eax, b_main
    setne al
    movzx eax, al
    mov @aux24, eax
    mov eax, a_main
    cmp eax, b_main
    je label_6
    mov eax, c_6_ul_ul
    mov k_main, eax
label_6:
    mov eax, k_main
    mov x_main, eax
    invoke ExitProcess, 0
end start
