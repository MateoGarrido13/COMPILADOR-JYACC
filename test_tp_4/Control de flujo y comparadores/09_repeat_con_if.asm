.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    i_main DD ?
    s_main DD ?
    c_1_ul_ul DD 1
    c_0_ul_ul DD 0
    c_5_ul_ul DD 5
    c_10_ul_ul DD 10
    @aux5 DD ?
    @aux7 DD ?
    @aux10 DD ?
    @aux12 DD ?
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
    mov eax, c_1_ul_ul
    mov i_main, eax
    mov eax, c_0_ul_ul
    mov s_main, eax
label_1:
    mov eax, i_main
    cmp eax, c_5_ul_ul
    setb al
    movzx eax, al
    mov @aux5, eax
    mov eax, i_main
    cmp eax, c_5_ul_ul
    jae label_2
    mov eax, s_main
    add eax, i_main
    jc error_overflow_suma
    mov @aux7, eax
    mov eax, @aux7
    mov s_main, eax
label_2:
    mov eax, i_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux10, eax
    mov eax, @aux10
    mov i_main, eax
    mov eax, i_main
    cmp eax, c_10_ul_ul
    setbe al
    movzx eax, al
    mov @aux12, eax
    mov eax, i_main
    cmp eax, c_10_ul_ul
    ja label_3
    jmp label_1
label_3:
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
