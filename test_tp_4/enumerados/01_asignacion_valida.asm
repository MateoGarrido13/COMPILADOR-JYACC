.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    c_3_ul_ul DD 3
    x_main DD ?
    ok DB "ok",0
    fmt_cadena DB "%s",0
    msg_enum_invalido DB "Error: asignacion invalida a variable de tipo enumeracion",0

.code
start:
    mov eax, c_2_ul_ul
    cmp eax, 1
    je enum_valido_0
    cmp eax, 2
    je enum_valido_0
    cmp eax, 3
    je enum_valido_0
    jmp error_enum_invalido
enum_valido_0:
    mov eax, c_2_ul_ul
    mov x_main, eax
    invoke crt_printf, offset fmt_cadena, offset ok
    jmp fin_normal
error_enum_invalido:
    invoke crt_printf, offset msg_enum_invalido
    invoke ExitProcess, 4
fin_normal:
    invoke ExitProcess, 0
end start
