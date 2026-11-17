.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    c_6_ul_ul DD 6
    c_1_ul_ul DD 1
    @aux7 DD ?
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
fn_foo_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    leave
    ret
fin_foo_main:
    leave
    ret
    mov eax, c_6_ul_ul
    mov x_main, eax
    ; (PAR, a:main:foo, x:main)
    sub esp, 4
    mov eax, x_main
    mov dword ptr [esp+0], eax
    call fn_foo_main
    add esp, 4
    mov @aux7, eax
    invoke crt_printf, offset fmt_ulongint, @aux7
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
