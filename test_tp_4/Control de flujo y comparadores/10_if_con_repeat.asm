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
    @aux10 DD ?
    @aux12 DD ?

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
    jbe label_3
label_1:
    mov eax, s_main
    add eax, x_main
    mov @aux8, eax
    mov eax, @aux8
    mov s_main, eax
    mov eax, x_main
    add eax, c_1_ul_ul
    mov @aux10, eax
    mov eax, @aux10
    mov x_main, eax
    mov eax, x_main
    cmp eax, c_5_ul_ul
    setb al
    movzx eax, al
    mov @aux12, eax
    mov eax, x_main
    cmp eax, c_5_ul_ul
    jae label_2
    jmp label_1
label_2:
    jmp label_4
label_3:
    mov eax, c_99_ul_ul
    mov s_main, eax
label_4:
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
