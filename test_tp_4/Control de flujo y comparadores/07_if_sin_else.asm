.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DD ?
    x_main DD ?
    c_5_ul_ul DD 5
    c_0_ul_ul DD 0
    c_3_ul_ul DD 3
    c_1_ul_ul DD 1
    c_10_ul_ul DD 10
    c_2_ul_ul DD 2
    @aux4 DD ?
    @aux8 DD ?

.code
start:
    mov eax, c_5_ul_ul
    mov a_main, eax
    mov eax, c_0_ul_ul
    mov x_main, eax
    mov eax, a_main
    cmp eax, c_3_ul_ul
    seta al
    movzx eax, al
    mov @aux4, eax
    mov eax, a_main
    cmp eax, c_3_ul_ul
    jbe label_1
    mov eax, c_1_ul_ul
    mov x_main, eax
label_1:
    mov eax, a_main
    cmp eax, c_10_ul_ul
    setae al
    movzx eax, al
    mov @aux8, eax
    mov eax, a_main
    cmp eax, c_10_ul_ul
    jb label_2
    mov eax, c_2_ul_ul
    mov x_main, eax
label_2:
    invoke ExitProcess, 0
end start
