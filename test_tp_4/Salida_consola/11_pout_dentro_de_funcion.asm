.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    dentro DB "dentro",0
    x_main DD ?
    c_2_ul_ul DD 2
    c_1_ul_ul DD 1
    @aux9 DD ?
    fmt_ulongint DB "%u",0
    fmt_cadena DB "%s",0

.code
start:
fn_eco_main:
    push ebp
    mov ebp, esp
    invoke crt_printf, offset fmt_cadena, offset dentro
    invoke crt_printf, offset fmt_ulongint, dword ptr [ebp+8]
    mov eax, dword ptr [ebp+8]
    leave
    ret
fin_eco_main:
    leave
    ret
    mov eax, c_2_ul_ul
    mov x_main, eax
    ; (PAR, a:main:eco, x:main)
    sub esp, 4
    mov eax, x_main
    mov dword ptr [esp+0], eax
    call fn_eco_main
    add esp, 4
    mov @aux9, eax
    invoke crt_printf, offset fmt_ulongint, @aux9
    invoke ExitProcess, 0
end start
