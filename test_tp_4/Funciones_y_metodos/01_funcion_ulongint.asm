.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    r_main DD ?
    c_3_ul_ul DD 3
    c_4_ul_ul DD 4
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    @aux3 DD ?
    @aux10 DD ?
    fmt_ulongint DB "%u",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
    jmp main_inicio
fn_suma_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    add eax, dword ptr [ebp+12]
    jc error_overflow_suma
    mov @aux3, eax
    mov eax, @aux3
    leave
    ret
fin_suma_main:
    leave
    ret
main_inicio:
    mov eax, c_3_ul_ul
    mov x_main, eax
    mov eax, c_4_ul_ul
    mov y_main, eax
    ; (PAR, a:main:suma, x:main)
    ; (PAR, b:main:suma, y:main)
    sub esp, 8
    mov eax, x_main
    mov dword ptr [esp+0], eax
    mov eax, y_main
    mov dword ptr [esp+4], eax
    call fn_suma_main
    add esp, 8
    mov @aux10, eax
    mov eax, @aux10
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
