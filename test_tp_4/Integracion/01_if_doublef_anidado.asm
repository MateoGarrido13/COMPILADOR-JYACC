.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DD ?
    b_main DD ?
    k_main DD ?
    d_main DQ ?
    c_5_ul_ul DD 5
    c_2_ul_ul DD 2
    c_1_5 DQ 1.5
    c_11_ul_ul DD 11
    c_12_ul_ul DD 12
    c_20_ul_ul DD 20
    @aux5 DQ ?
    @aux6 DD ?
    @aux8 DD ?
    @fpu_status DW ?
    fmt_ulongint DB "%u",0

.code
start:
    mov eax, c_5_ul_ul
    mov a_main, eax
    mov eax, c_2_ul_ul
    mov b_main, eax
    fld c_1_5
    fstp d_main
    fild a_main
    fstp @aux5
    fld @aux5
    fcomp d_main
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    seta al
    movzx eax, al
    mov @aux6, eax
    fld @aux5
    fcomp d_main
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    jbe label_3
    mov eax, b_main
    cmp eax, c_2_ul_ul
    setae al
    movzx eax, al
    mov @aux8, eax
    mov eax, b_main
    cmp eax, c_2_ul_ul
    jb label_1
    mov eax, c_11_ul_ul
    mov k_main, eax
    jmp label_2
label_1:
    mov eax, c_12_ul_ul
    mov k_main, eax
label_2:
    jmp label_4
label_3:
    mov eax, c_20_ul_ul
    mov k_main, eax
label_4:
    invoke crt_printf, offset fmt_ulongint, k_main
    invoke ExitProcess, 0
end start
