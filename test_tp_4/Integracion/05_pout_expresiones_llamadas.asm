.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    c_2_ul_ul DD 2
    c_3_ul_ul DD 3
    resultado DB "resultado=",0
    c_1_ul_ul DD 1
    @aux3 DD ?
    @aux11 DD ?
    @aux13 DD ?
    @aux14 DD ?
    @aux16 DQ ?
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0
    fmt_cadena DB "%s",0

.code
start:
fn_suma_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    add eax, dword ptr [ebp+12]
    mov @aux3, eax
    mov eax, @aux3
    leave
    ret
fin_suma_main:
    leave
    ret
    mov eax, c_2_ul_ul
    mov x_main, eax
    mov eax, c_3_ul_ul
    mov y_main, eax
    invoke crt_printf, offset fmt_cadena, offset resultado
    ; (PAR, a:main:suma, x:main)
    ; (PAR, b:main:suma, y:main)
    sub esp, 8
    mov eax, x_main
    mov dword ptr [esp+0], eax
    mov eax, y_main
    mov dword ptr [esp+4], eax
    call fn_suma_main
    add esp, 8
    mov @aux11, eax
    invoke crt_printf, offset fmt_ulongint, @aux11
    mov eax, x_main
    imul eax, y_main
    mov @aux13, eax
    mov eax, @aux13
    add eax, c_1_ul_ul
    mov @aux14, eax
    invoke crt_printf, offset fmt_ulongint, @aux14
    fild x_main
    fstp @aux16
    invoke crt_printf, offset fmt_doublef, @aux16
    invoke ExitProcess, 0
end start
