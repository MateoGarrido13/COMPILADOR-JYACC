.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    w_main DD ?
    z_main DQ ?
    c_2_ul_ul DD 2
    c_4_ul_ul DD 4
    c_6_ul_ul DD 6
    p1_main DD ?
    p2_main DD ?
    a_main_base DD ?
    pa_main_base_m DD ?
    b_main_hija DD ?
    n_main_foo DD ?
    d_main_foo DQ ?
    c_0_ul_ul DD 0
    c_4294967295_ul_ul DD 4294967295
    c_1_0 DQ 1.0
    c_6d_neg_1 DQ .6d-1
    c_1_ul_ul DD 1
    ok DB "ok",0
    c_2_5 DQ 2.5
    c_3_ul_ul DD 3
    hola DB "hola",0
    @aux16 DD ?
    @aux18 DD ?
    @aux19 DD ?
    @aux20 DD ?
    @aux21 DD ?
    @aux23 DD ?
    @aux24 DD ?
    @aux28 DQ ?
    @aux32 DD ?
    @aux36 DD ?
    @aux38 DD ?
    @aux42 DD ?

.code
start:
    ; (PROGRAMA, --, --)
    ; (CLASE, base, --)
    ; (METODO, m, --)
    ; (ret, pa:main:base:m, --)
    ; (FIN_METODO, m, --)
    ; (FIN_CLASE, base, --)
    ; (CLASE, hija, --)
    ; (FIN_CLASE, hija, --)
    ; (FUNCION, foo, --)
    ; (ret, n:main:foo, --)
    ; (FIN_FUNCION, foo, --)
    mov eax, c_0_ul_ul
    mov x_main, eax
    mov eax, c_4294967295_ul_ul
    mov y_main, eax
    fld c_1_0
    fstp z_main
    fld c_6d_neg_1
    fstp z_main
    mov eax, c_2_ul_ul
    mov y_main, eax
    mov eax, @aux16
    mov x_main, eax
    mov eax, x_main
    add eax, y_main
    mov @aux18, eax
    mov eax, @aux18
    mov y_main, eax
    mov eax, c_1_ul_ul
    mov w_main, eax
    mov eax, @aux19
    imul eax, @aux20
    mov @aux21, eax
    mov eax, @aux21
    mov x_main, eax
    mov eax, c_1_ul_ul
    mov x_main, eax
    mov eax, @aux23
    cmp eax, c_1_ul_ul
    sete al
    movzx eax, al
    mov @aux24, eax
    mov eax, @aux23
    cmp eax, c_1_ul_ul
    jne label_1
    ; (pout, ok, --)
label_1:
    fild x_main
    fstp @aux28
    fld @aux28
    fstp z_main
    ; (PAR, n:main:foo, 1$ul)
    ; (PAR, d:main:foo, 2.5)
    ; (CALL, foo:main, 2)
    mov eax, @aux32
    mov x_main, eax
    mov eax, c_2_ul_ul
    mov p1_main, eax
label_2:
    mov eax, x_main
    add eax, c_1_ul_ul
    mov @aux36, eax
    mov eax, @aux36
    mov x_main, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    setb al
    movzx eax, al
    mov @aux38, eax
    mov eax, x_main
    cmp eax, c_3_ul_ul
    jae label_3
    jmp label_2
label_3:
    mov eax, x_main
    cmp eax, c_0_ul_ul
    seta al
    movzx eax, al
    mov @aux42, eax
    mov eax, x_main
    cmp eax, c_0_ul_ul
    jbe label_4
    ; (pout, hola, --)
    jmp label_5
label_4:
    ; (pout, x:main, --)
label_5:
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
