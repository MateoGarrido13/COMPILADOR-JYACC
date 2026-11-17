.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib
dll_dllcrt0 PROTO C
printf PROTO C : VARARG

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
    @tp4_fmt_uint DB "%u",10,0
    @tp4_fmt_double DB "%.20Lf",10,0
    @tp4_fmt_string DB "%s",10,0

.code
start:
    jmp @tp4_main

@tp4_main:
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
    mov @aux7, eax
    mov eax, @aux7
    mov y_main, eax
    mov eax, y_main
    cmp eax, x_main
    setb al
    movzx eax, al
    mov @aux9, eax
    cmp @aux9, 0
    je label_3
    jmp label_2
label_3:
    mov eax, x_main
    add eax, c_1_ul_ul
    mov @aux13, eax
    mov eax, @aux13
    mov x_main, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    setb al
    movzx eax, al
    mov @aux15, eax
    cmp @aux15, 0
    je label_4
    jmp label_1
label_4:
    mov eax, x_main
    add eax, y_main
    mov @aux19, eax
    mov eax, @aux19
    mov z_main, eax
    invoke ExitProcess, 0
end start
