.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    r_main DD ?
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    @aux7 DD ?
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
fn_elegir_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    leave
    ret
fin_elegir_main:
    leave
    ret
    ; (PAR, a:main:elegir, 2$ul)
    ; (PAR, b:main:elegir, 1$ul)
    sub esp, 8
    mov eax, c_2_ul_ul
    mov dword ptr [esp+0], eax
    mov eax, c_1_ul_ul
    mov dword ptr [esp+4], eax
    call fn_elegir_main
    add esp, 8
    mov @aux7, eax
    mov eax, @aux7
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
