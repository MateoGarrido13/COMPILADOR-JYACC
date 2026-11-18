.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    z_main DQ ?
    c_1_ul_ul DD 1
    c_0_5 DQ 0.5
    c_1_0 DQ 1.0
    c_0_0 DQ 0.0
    @aux3 DQ ?
    @aux4 DD ?
    @fpu_status DW ?

.code
start:
    mov eax, c_1_ul_ul
    mov x_main, eax
    fild x_main
    fstp @aux3
    fld @aux3
    fcomp c_0_5
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    seta al
    movzx eax, al
    mov @aux4, eax
    fld @aux3
    fcomp c_0_5
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    jbe label_1
    fld c_1_0
    fstp z_main
    jmp label_2
label_1:
    fld c_0_0
    fstp z_main
label_2:
    invoke ExitProcess, 0
end start
