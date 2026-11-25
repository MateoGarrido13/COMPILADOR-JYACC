.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    a_main DQ ?
    b_main DQ ?
    r_main DQ ?
    c_1_5 DQ 1.5
    c_2_0 DQ 2.0
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    @aux3 DQ ?
    @aux10 DQ ?
    fmt_doublef DB "%f",0
    msg_overflow_float DB "Error: overflow en producto de doublef",0

.code
start:
    jmp main_inicio
fn_escala_main:
    push ebp
    mov ebp, esp
    fld qword ptr [ebp+8]
    fmul qword ptr [ebp+16]
    fstp @aux3
    fld @aux3
    fxam
    fstsw ax
    fstp st(0)
    sahf
    jc error_overflow_float
    fld @aux3
    leave
    ret
fin_escala_main:
    leave
    ret
main_inicio:
    fld c_1_5
    fstp a_main
    fld c_2_0
    fstp b_main
    ; (PAR, x:main:escala, a:main)
    ; (PAR, y:main:escala, b:main)
    sub esp, 16
    mov eax, dword ptr [a_main+4]
    mov dword ptr [esp+4], eax
    mov eax, dword ptr [a_main]
    mov dword ptr [esp+0], eax
    mov eax, dword ptr [b_main+4]
    mov dword ptr [esp+12], eax
    mov eax, dword ptr [b_main]
    mov dword ptr [esp+8], eax
    call fn_escala_main
    add esp, 16
    fstp @aux10
    fld @aux10
    fstp r_main
    invoke crt_printf, offset fmt_doublef, r_main
    jmp fin_normal
error_overflow_float:
    invoke crt_printf, offset msg_overflow_float
    invoke ExitProcess, 2
fin_normal:
    invoke ExitProcess, 0
end start
