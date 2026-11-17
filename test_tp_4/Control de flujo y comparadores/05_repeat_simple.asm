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

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_0_ul_ul
    mov x_main, eax
label_1:
    mov eax, x_main
    add eax, c_1_ul_ul
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
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
