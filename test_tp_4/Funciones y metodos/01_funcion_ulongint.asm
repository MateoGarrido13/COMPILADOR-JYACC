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

.code
start:
    ; (PROGRAMA, --, --)
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
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
