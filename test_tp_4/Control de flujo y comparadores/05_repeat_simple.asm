.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    c_0_ul_ul DD 0
    c_1_ul_ul DD 1
    c_3_ul_ul DD 3
    @aux4 DD ?
    @aux6 DD ?
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
    mov eax, c_0_ul_ul
    mov x_main, eax
label_1:
    mov eax, x_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux4, eax
    mov eax, @aux4
    mov x_main, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    setb al
    movzx eax, al
    mov @aux6, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    jae label_2
    jmp label_1
label_2:
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
