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
    b_main_hija DD ?
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
    fmt_ulongint DB "%u",0
    fmt_cadena DB "%s",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0
    msg_enum_invalido DB "Error: asignacion invalida a variable de tipo enumeracion",0

.code
start:
    jmp main_inicio
mt_m_main_base:
    push ebp
    mov ebp, esp
    ; this: dword ptr [ebp+8]
    mov eax, dword ptr [ebp+12]
    leave
    ret
fin_m_main_base:
    leave
    ret
fn_foo_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    leave
    ret
fin_foo_main:
    leave
    ret
main_inicio:
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
    jc error_overflow_suma
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
    invoke crt_printf, offset fmt_cadena, offset ok
label_1:
    fild x_main
    fstp @aux28
    fld @aux28
    fstp z_main
    ; (PAR, n:main:foo, 1$ul)
    ; (PAR, d:main:foo, 2.5)
    sub esp, 12
    mov eax, c_1_ul_ul
    mov dword ptr [esp+0], eax
    mov eax, dword ptr [c_2_5+4]
    mov dword ptr [esp+8], eax
    mov eax, dword ptr [c_2_5]
    mov dword ptr [esp+4], eax
    call fn_foo_main
    add esp, 12
    mov @aux32, eax
    mov eax, @aux32
    mov x_main, eax
    mov eax, c_2_ul_ul
    cmp eax, 2
    je enum_valido_0
    cmp eax, 4
    je enum_valido_0
    cmp eax, 6
    je enum_valido_0
    jmp error_enum_invalido
enum_valido_0:
    mov eax, c_2_ul_ul
    mov p1_main, eax
label_2:
    mov eax, x_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
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
    invoke crt_printf, offset fmt_cadena, offset hola
    jmp label_5
label_4:
    invoke crt_printf, offset fmt_ulongint, x_main
label_5:
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
error_enum_invalido:
    invoke crt_printf, offset msg_enum_invalido
    invoke ExitProcess, 4
fin_normal:
    invoke ExitProcess, 0
end start
