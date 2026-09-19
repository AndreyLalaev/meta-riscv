baudrate=115200
consoledev=ttyS0
optargs=earlycon=sbi loglevel=9 rootwait rw
root=root=/dev/mmcblk0p2
setbootargs=setenv bootargs ${root} console=${consoledev},${baudrate} ${optargs}

fdt=default.dtb
kernel=uImage.fit

loadfdt=fatload mmc 0:1 ${fdt_addr_r} ${fdt}
loaduimage=fatload mmc 0:1 ${kernel_comp_addr_r} ${kernel}

run loaduimage
run loadfdt
run setbootargs

bootm ${kernel_comp_addr_r} - ${fdt_addr_r}
