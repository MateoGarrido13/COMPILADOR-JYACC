.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    i_main DD ?
    j_main DD ?
    acc_main DD ?
    c_0_ul_ul DD 0
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    c_4_ul_ul DD 4
    @aux5 DD ?
    @aux9 DD ?
    @aux11 DD ?
    @aux15 DD ?
    @aux17 DD ?
    @aux21 DD ?
    @aux24 DD ?
    @aux29 DD ?
    fmt_ulongint DB "%u",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
    mov eax, c_0_ul_ul
    mov i_main, eax
    mov eax, c_0_ul_ul
    mov acc_main, eax
label_1:
    mov eax, i_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux5, eax
    mov eax, @aux5
    mov i_main, eax
    mov eax, c_0_ul_ul
    mov j_main, eax
label_2:
    mov eax, j_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux9, eax
    mov eax, @aux9
    mov j_main, eax
    mov eax, j_main
    cmp eax, i_main
    setb al
    movzx eax, al
    mov @aux11, eax
    mov eax, j_main
    cmp eax, i_main
    jae label_3
    jmp label_2
label_3:
    mov eax, i_main
    cmp eax, c_2_ul_ul
    setae al
    movzx eax, al
    mov @aux15, eax
    mov eax, i_main
    cmp eax, c_2_ul_ul
    jb label_4
    mov eax, acc_main
    add eax, i_main
    jc error_overflow_suma
    mov @aux17, eax
    mov eax, @aux17
    mov acc_main, eax
    jmp label_5
label_4:
    mov eax, acc_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux21, eax
    mov eax, @aux21
    mov acc_main, eax
label_5:
    mov eax, i_main
    cmp eax, c_4_ul_ul
    setb al
    movzx eax, al
    mov @aux24, eax
    mov eax, i_main
    cmp eax, c_4_ul_ul
    jae label_6
    jmp label_1
label_6:
    invoke crt_printf, offset fmt_ulongint, acc_main
    mov eax, i_main
    add eax, acc_main
    jc error_overflow_suma
    mov @aux29, eax
    invoke crt_printf, offset fmt_ulongint, @aux29
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
