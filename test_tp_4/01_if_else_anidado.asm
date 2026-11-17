.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    z_main DD ?
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    c_0_ul_ul DD 0
    c_9_ul_ul DD 9
    @aux5 DD ?
    @aux7 DD ?
    @aux9 DD ?
    @aux13 DD ?

.code
start:
    ; (PROGRAMA, --, --)
    mov eax, c_1_ul_ul
    mov x_main, eax
    mov eax, c_2_ul_ul
    mov y_main, eax
    mov eax, c_0_ul_ul
    mov z_main, eax
    ; (<, x:main, y:main)
    ; (BF, [5], label_3)
    ; (==, z:main, 0$ul)
    ; (BF, [7], label_1)
    mov eax, x_main
    add eax, y_main
    mov @aux9, eax
    mov eax, @aux9
    mov z_main, eax
    ; (BI, label_2, --)
    ; (label_1, --, --)
    mov eax, y_main
    sub eax, x_main
    mov @aux13, eax
    mov eax, @aux13
    mov z_main, eax
    ; (label_2, --, --)
    ; (BI, label_4, --)
    ; (label_3, --, --)
    mov eax, c_9_ul_ul
    mov z_main, eax
    ; (label_4, --, --)
    ; (pout, z:main, --)
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
