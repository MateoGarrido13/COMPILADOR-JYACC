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
    @aux7 DD ?
    @aux9 DD ?
    @aux13 DD ?
    @aux15 DD ?
    @aux19 DD ?
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
    mov eax, c_0_ul_ul
    mov x_main, eax
    mov eax, c_0_ul_ul
    mov y_main, eax
label_1:
    mov eax, c_0_ul_ul
    mov y_main, eax
label_2:
    mov eax, y_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux7, eax
    mov eax, @aux7
    mov y_main, eax
    mov eax, y_main
    cmp eax, x_main
    setb al
    movzx eax, al
    mov @aux9, eax
    mov eax, y_main
    cmp eax, x_main
    jae label_3
    jmp label_2
label_3:
    mov eax, x_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux13, eax
    mov eax, @aux13
    mov x_main, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    setb al
    movzx eax, al
    mov @aux15, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    jae label_4
    jmp label_1
label_4:
    mov eax, x_main
    add eax, y_main
    jc error_overflow_suma
    mov @aux19, eax
    mov eax, @aux19
    mov z_main, eax
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
