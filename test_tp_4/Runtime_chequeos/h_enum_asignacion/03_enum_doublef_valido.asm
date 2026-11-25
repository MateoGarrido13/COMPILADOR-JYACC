.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    c_1_5 DQ 1.5
    c_2_5 DQ 2.5
    m_main DQ ?
    v_main DQ ?
    @fpu_status DW ?
    fmt_doublef DB "%f",0
    msg_enum_invalido DB "Error: asignacion invalida a variable de tipo enumeracion",0

.code
start:
    jmp main_inicio
main_inicio:
    fld c_2_5
    fstp v_main
    fld v_main
    fcomp c_1_5
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    jp error_enum_invalido
    jz enum_valido_0
    fld v_main
    fcomp c_2_5
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    jp error_enum_invalido
    jz enum_valido_0
    jmp error_enum_invalido
enum_valido_0:
    fld v_main
    fstp m_main
    invoke crt_printf, offset fmt_doublef, m_main
    jmp fin_normal
error_enum_invalido:
    invoke crt_printf, offset msg_enum_invalido
    invoke ExitProcess, 4
fin_normal:
    invoke ExitProcess, 0
end start
