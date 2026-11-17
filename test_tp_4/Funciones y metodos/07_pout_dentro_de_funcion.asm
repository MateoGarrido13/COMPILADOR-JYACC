.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    dentro DB "dentro",0
    x_main DD ?
    c_7_ul_ul DD 7
    c_1_ul_ul DD 1
    @aux8 DD ?
    fmt_ulongint DB "%u",0
    fmt_cadena DB "%s",0

.code
start:
    ; (PROGRAMA, --, --)
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
    ; (PAR, a:main:eco, 7$ul)
    sub esp, 4
    mov eax, c_7_ul_ul
    mov dword ptr [esp+0], eax
    call fn_eco_main
    add esp, 4
    mov @aux8, eax
    mov eax, @aux8
    mov x_main, eax
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
