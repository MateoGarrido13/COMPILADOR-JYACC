.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    c_0_ul_ul DD 0
    c_1_ul_ul DD 1
    c_3_ul_ul DD 3
    tres_o_mas DB "tres o mas",0
    c_5_ul_ul DD 5
    @aux4 DD ?
    @aux6 DD ?
    @aux13 DD ?
    fmt_ulongint DB "%u",0
    fmt_cadena DB "%s",0

.code
start:
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
    setae al
    movzx eax, al
    mov @aux6, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    jb label_2
    invoke crt_printf, offset fmt_cadena, offset tres_o_mas
    jmp label_3
label_2:
    invoke crt_printf, offset fmt_ulongint, x_main
label_3:
    mov eax, x_main
    cmp eax, c_5_ul_ul
    setb al
    movzx eax, al
    mov @aux13, eax
    mov eax, x_main
    cmp eax, c_5_ul_ul
    jae label_4
    jmp label_1
label_4:
    invoke ExitProcess, 0
end start
