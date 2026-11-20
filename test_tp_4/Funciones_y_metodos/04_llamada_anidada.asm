.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    c_2_ul_ul DD 2
    r_main DD ?
    c_3_ul_ul DD 3
    c_1_ul_ul DD 1
    c_5_ul_ul DD 5
    @aux3 DD ?
    @aux7 DD ?
    @aux11 DD ?
    @aux14 DD ?
    fmt_ulongint DB "%u",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
fn_doble_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    imul eax, c_2_ul_ul
    mov @aux3, eax
    mov eax, @aux3
    leave
    ret
fin_doble_main:
    leave
    ret
fn_combinar_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    add eax, dword ptr [ebp+12]
    jc error_overflow_suma
    mov @aux7, eax
    mov eax, @aux7
    leave
    ret
fin_combinar_main:
    leave
    ret
    ; (PAR, n:main:doble, 3$ul)
    sub esp, 4
    mov eax, c_3_ul_ul
    mov dword ptr [esp+0], eax
    call fn_doble_main
    add esp, 4
    mov @aux11, eax
    ; (PAR, a:main:combinar, [11])
    ; (PAR, b:main:combinar, 5$ul)
    sub esp, 8
    mov eax, @aux11
    mov dword ptr [esp+0], eax
    mov eax, c_5_ul_ul
    mov dword ptr [esp+4], eax
    call fn_combinar_main
    add esp, 8
    mov @aux14, eax
    mov eax, @aux14
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
