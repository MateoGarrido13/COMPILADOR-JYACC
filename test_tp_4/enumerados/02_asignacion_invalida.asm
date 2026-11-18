.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    c_3_ul_ul DD 3
    x_main DD ?
    y_main DD ?
    c_5_ul_ul DD 5
    msg_enum_invalido DB "Error: asignacion invalida a variable de tipo enumeracion",0

.code
start:
    mov eax, c_5_ul_ul
    mov y_main, eax
    mov eax, y_main
    cmp eax, 1
    je enum_valido_0
    cmp eax, 2
    je enum_valido_0
    cmp eax, 3
    je enum_valido_0
    jmp error_enum_invalido
enum_valido_0:
    mov eax, y_main
    mov x_main, eax
    jmp fin_normal
error_enum_invalido:
    invoke crt_printf, offset msg_enum_invalido
    invoke ExitProcess, 4
fin_normal:
    invoke ExitProcess, 0
end start
